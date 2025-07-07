module com.github.romanqed.jsm {
    // Imports
    requires org.objectweb.asm;
    requires com.github.romanqed.jfunc;
    requires com.github.romanqed.asm.sorter;
    requires com.github.romanqed.switchgen;
    requires com.github.romanqed.jeflect.loader;
    // Exports
    exports com.github.romanqed.jsm;
    exports com.github.romanqed.jsm.model;
    exports com.github.romanqed.jsm.asm;
}
