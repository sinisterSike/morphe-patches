package app.template.patches.droidlab

import app.morphe.patcher.annotation.Patch
import app.morphe.patcher.patch.BytecodePatch
import app.morphe.patcher.fingerprint.method.impl.MethodFingerprint
import app.morphe.patcher.util.smali.types.SmaliParameter
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.instructions.*
import com.android.tools.smali.dexlib2.immutable.reference.MethodReference
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import java.lang.reflect.Modifier

@Patch(
    name = "Premium Injector",
    description = "Unlocks premium",
    default=true,
    dependencies = []
)
object PremiumSharedPrefsInjectorPatch : BytecodePatch() {

    // 1. Find MainActivity.onCreate dynamically via fingerprint layout
    private val onCreateFingerprint = MethodFingerprint(
        className = "Lcom/lightsourcelabs/droidlab/MainActivity;",
        customName = "onCreate",
        parameters = listOf(SmaliParameter("Landroid/os/Bundle;")),
        returnType = "V"
    )

    override fun execute() {
        // --- STEP A: DEFINE AND ADD THE NEW STANDALONE INJECTOR CLASS ---
        val injectorClassName = "Lcom/lightsourcelabs/droidlab/PremiumInjector;"
        
        // Construct bytecode instructions for PremiumInjector.inject(Context)
        val injectorInstructions = listOf(
            ImmutableInstruction22c(Opcode.CONST_STRING, 0, context.currentClass.stringSection.get("FlutterSharedPreferences")),
            ImmutableInstruction11n(Opcode.CONST_4, 1, 0),
            ImmutableInstruction35c(Opcode.INVOKE_VIRTUAL, 2, 0, 1, 0, 0, 0, 
                MethodReference("Landroid/content/Context;", "getSharedPreferences", listOf("Ljava/lang/String;", "I"), "Landroid/content/SharedPreferences;")),
            ImmutableInstruction00x(Opcode.MOVE_RESULT_OBJECT, 0),
            ImmutableInstruction35c(Opcode.INVOKE_INTERFACE, 1, 0, 0, 0, 0, 0, 
                MethodReference("Landroid/content/SharedPreferences;", "edit", emptyList(), "Landroid/content/SharedPreferences$Editor;")),
            ImmutableInstruction00x(Opcode.MOVE_RESULT_OBJECT, 0),
            ImmutableInstruction22c(Opcode.CONST_STRING, 1, context.currentClass.stringSection.get("flutter.droidlab_is_premium")),
            ImmutableInstruction11n(Opcode.CONST_4, 2, 1),
            ImmutableInstruction35c(Opcode.INVOKE_INTERFACE, 3, 0, 1, 2, 0, 0, 
                MethodReference("Landroid/content/SharedPreferences$Editor;", "putBoolean", listOf("Ljava/lang/String;", "Z"), "Landroid/content/SharedPreferences$Editor;")),
            ImmutableInstruction00x(Opcode.MOVE_RESULT_OBJECT, 0),
            ImmutableInstruction22c(Opcode.CONST_STRING, 1, context.currentClass.stringSection.get("droidlab_is_premium")),
            ImmutableInstruction35c(Opcode.INVOKE_INTERFACE, 3, 0, 1, 2, 0, 0, 
                MethodReference("Landroid/content/SharedPreferences$Editor;", "putBoolean", listOf("Ljava/lang/String;", "Z"), "Landroid/content/SharedPreferences$Editor;")),
            ImmutableInstruction00x(Opcode.MOVE_RESULT_OBJECT, 0),
            ImmutableInstruction35c(Opcode.INVOKE_INTERFACE, 1, 0, 0, 0, 0, 0, 
                MethodReference("Landroid/content/SharedPreferences$Editor;", "apply", emptyList(), "V")),
            ImmutableInstruction00x(Opcode.RETURN_VOID)
        )

        val injectMethod = ImmutableMethod(
            injectorClassName,
            "inject",
            listOf(SmaliParameter("Landroid/content/Context;")),
            "V",
            Modifier.PUBLIC or Modifier.STATIC,
            emptyList(),
            ImmutableMethodImplementation(3, injectorInstructions, emptyList(), emptyList())
        )

        // Inject this generated synthetic class definition directly into the app payload
        context.currentClass.addClass(injectorClassName, listOf(injectMethod))

        // --- STEP B: HOOK THE CALL SAFELY INTO MAINACTIVITY.ONCREATE ---
        val result = onCreateFingerprint.resolve(context.currentClass) ?: return
        val methodImplementation = result.method.implementation as? MutableMethodImplementation ?: return

        // Generate call to: invoke-static {p0}, Lcom/lightsourcelabs/droidlab/PremiumInjector;->inject(Landroid/content/Context;)V
        // In a typical onCreate implementation, p0 represents parameter register index (totalRegisters - parametersCount)
        val p0Register = methodImplementation.registerCount - 2 

        val hookInstruction = BuilderInstruction35c(
            Opcode.INVOKE_STATIC,
            1,            // Number of parameters passed (1 register)
            p0Register,   // Context parameter mapping register (p0)
            0, 0, 0, 0,   // Empty padding slots
            MethodReference(injectorClassName, "inject", listOf("Landroid/content/Context;"), "V")
        )

        // Add the hook to the very front of the existing instruction queue safely
        methodImplementation.instructions.add(0, hookInstruction)
    }
}
