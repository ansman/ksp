import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import java.io.OutputStreamWriter

class TestProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allFiles = resolver.getAllFiles().map { it.fileName }
        logger.warn(allFiles.toList().toString())
        if (invoked) {
            return emptyList()
        }
        invoked = true

        codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "Foo", "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write("package com.example\n\n")
                writer.write("actual class Foo {\n")

                val visitor = ClassVisitor()
                resolver.getAllFiles().forEach {
                    it.accept(visitor, writer)
                }

                writer.write("}\n")
            }
        }
        return emptyList()
    }
}

class ClassVisitor : KSTopDownVisitor<OutputStreamWriter, Unit>() {
    override fun defaultHandler(node: KSNode, data: OutputStreamWriter) {
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: OutputStreamWriter
    ) {
        super.visitClassDeclaration(classDeclaration, data)
        val symbolName = classDeclaration.simpleName.asString().lowercase()
        data.write("    actual val $symbolName = true\n")
    }
}

class TestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestProcessor(environment.codeGenerator, environment.logger)
    }
}
