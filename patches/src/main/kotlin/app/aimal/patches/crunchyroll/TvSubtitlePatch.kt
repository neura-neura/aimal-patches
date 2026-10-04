package app.aimal.patches.crunchyroll

import app.aimal.patches.subtitles.media3SubtitlePatch
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

val crunchyrollTvSubtitlesPatch = bytecodePatch(
    name = "Subtitle styling (Android TV)",
    description = "All Noir subtitle options with live preview and hot reload. Hold OK or press Menu during playback to open the editor; includes Fit/Stretch.",
    default = false,
) {
    compatibleWith(CRUNCHYROLL_TV)
    dependsOn(media3SubtitlePatch)
    extendWith("extensions/extension.mpe")
    execute {
        val activity = mutableClassDefByOrNull {
            it.type == "Lcom/crunchyroll/crunchyroid/player/ui/PlayerActivity;"
        } ?: throw PatchException("Cannot find Crunchyroll Android TV PlayerActivity.")
        val original = activity.methods.singleOrNull {
            it.name == "dispatchKeyEvent" && it.parameterTypes == listOf("Landroid/view/KeyEvent;") && it.returnType == "Z"
        }
        original?.setName("aimalOriginalDispatchKeyEvent")
        val fallback = if (original == null) {
            "invoke-super { p0, p1 }, ${activity.superclass}->dispatchKeyEvent(Landroid/view/KeyEvent;)Z"
        } else {
            "invoke-virtual { p0, p1 }, ${activity.type}->aimalOriginalDispatchKeyEvent(Landroid/view/KeyEvent;)Z"
        }
        val wrapper = MutableMethod(ImmutableMethod(activity.type, "dispatchKeyEvent",
            listOf(ImmutableMethodParameter("Landroid/view/KeyEvent;", emptySet(), null)),
            "Z", AccessFlags.PUBLIC.value, emptySet(), emptySet(),
            ImmutableMethodImplementation(3, emptyList(), emptyList(), emptyList())))
        wrapper.addInstructions("""
            invoke-static { p0, p1 }, Lapp/aimal/extension/crunchyroll/TvSubtitleHelper;->onKey(Landroid/app/Activity;Landroid/view/KeyEvent;)Z
            move-result v0
            if-eqz v0, :native
            return v0
            :native
            $fallback
            move-result v0
            return v0
        """)
        activity.methods.add(wrapper)
    }
}
