package app.aimal.patches.crunchyroll

import app.aimal.patches.subtitles.hookCaptionDraw
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation

private const val STYLER = "Lapp/aimal/extension/crunchyroll/SubtitleStyler;"

@Suppress("unused")
val subtitleStylePatch = bytecodePatch(
    name = "Subtitle styling",
    description = "Adds all Noir subtitle style options, a live example and immediate updates during playback.",
    default = true,
) {
    compatibleWith(CRUNCHYROLL)
    dependsOn(aspectRatioPatch)
    extendWith("extensions/extension.mpe")

    execute {
        val load = SubtitlesLoadTrackFingerprint.method
        load.addInstructions(0, """
            invoke-static/range { p0 .. p1 }, $STYLER->capture(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
            move-result-object p1
        """)
        val renderer = mutableClassDefBy(load.definingClass)
        // Fail explicitly if the expected clock is absent instead of silently
        // presenting settings that cannot update the real captions.
        val render = renderer.methods.singleOrNull {
            it.name in setOf("render", "renderFrame") && it.implementation != null &&
                it.parameterTypes.any { parameter -> parameter == "J" } &&
                it.returnType.startsWith("L") && !AccessFlags.STATIC.isSet(it.accessFlags)
        } ?: throw PatchException("Cannot identify Crunchyroll's ASS render clock. This version needs a new renderer fingerprint.")
        val timestampIndex = render.parameterTypes.indexOfLast { it == "J" }
        val timeRegister = 1 + render.parameterTypes.take(timestampIndex).sumOf { if (it == "J" || it == "D") 2 else 1 }
        val argumentRegisters = 1 + render.parameterTypes.sumOf { if (it == "J" || it == "D") 2 else 1 }
        val name = render.name
        render.setName("aimalOriginalRender")
        val wrapper = MutableMethod(ImmutableMethod(renderer.type, name, render.parameters,
            render.returnType, render.accessFlags, render.annotations, render.hiddenApiRestrictions,
            ImmutableMethodImplementation(argumentRegisters + 3, emptyList(), emptyList(), emptyList())))
        wrapper.addInstructions("""
            move-object/from16 v0, p0
            move-wide/from16 v1, p$timeRegister
            invoke-static { v0, v1, v2 }, $STYLER->onRender(Ljava/lang/Object;J)V
            invoke-virtual/range { p0 .. p${argumentRegisters - 1} }, ${renderer.type}->aimalOriginalRender(${render.parameterTypes.joinToString("")})${render.returnType}
            move-result-object v0
            return-object v0
        """)
        renderer.methods.add(wrapper)
        renderer.methods.filter { it.name in setOf("release", "releaseTrack", "unloadTrack", "clearTrack", "close") &&
            it.implementation != null && !AccessFlags.STATIC.isSet(it.accessFlags) }.forEach {
            it.addInstruction(0, "invoke-static/range { p0 .. p0 }, $STYLER->clear(Ljava/lang/Object;)V")
        }
        val captionView = mutableClassDefByOrNull { clazz ->
            clazz.fields.any { it.type.contains("AssFrame") } &&
                clazz.methods.any { it.name == "onDraw" && it.parameterTypes == listOf("Landroid/graphics/Canvas;") }
        } ?: throw PatchException("Cannot find Crunchyroll's ASS caption View. This version needs a new draw fingerprint.")
        captionView.hookCaptionDraw("onDraw", STYLER)
    }
}
