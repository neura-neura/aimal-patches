package app.aimal.patches.crunchyroll

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

val crunchyrollTvDisableUpdatesPatch = bytecodePatch(
    name = "Disable forced updates (Android TV)",
    description = "Skip the local minimum-version update gate. Server compatibility is still required.",
    default = false,
) {
    compatibleWith(Compatibility(
        name = "Crunchyroll (Android TV)",
        packageName = "com.crunchyroll.crunchyroid",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xF47521,
        targets = listOf(AppTarget(version = "3.81.0")),
    ))
    execute {
        val compat = mutableClassDefByOrNull {
            it.type == "Lcom/crunchyroll/core/compatibility/DeviceCompatImpl;"
        } ?: throw PatchException("Cannot find the TV version compatibility check.")
        val check = compat.methods.singleOrNull { method ->
            method.parameterTypes == listOf("Ljava/lang/String;") && method.returnType == "Z" &&
                method.implementation?.instructions?.any {
                    ((it as? ReferenceInstruction)?.reference as? StringReference)?.string
                        ?.startsWith("ForceUpdate: are versions min:") == true
                } == true
        } ?: throw PatchException("Cannot identify the TV minimum-version gate.")
        // Only the minimum-version predicate changes. Device support, account
        // authentication, entitlement checks and stream configuration stay native.
        check.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
    }
}
