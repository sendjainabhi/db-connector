import java.nio.file.*
import groovy.io.FileType
import java.io.File

def cancellaCartella(Path cartella) {
    try {

        def deleteFilesAndDirs = { File dir ->
            dir.traverse(type: groovy.io.FileType.FILES) { file ->
                file.delete()
                println "Deleted: ${file.absolutePath}"
            }
            
            dir.deleteDir()
            println "Deleted directory: ${dir.absolutePath}"
        }

        def rootDir = new File(cartella.toString())

        if (rootDir.exists() && rootDir.isDirectory()) {
            deleteFilesAndDirs(rootDir)
            println "Directory and its contents deleted successfully."
        } else {
            println "Specified directory does not exist or is not a directory."
        }

        println "Cartella cancellata con successo: ${cartella}"
    } catch (IOException e) {
        println "Errore durante la cancellazione della cartella: ${e.message}"
    }
}


def copiaFile(sourceDir, destDir, fileName) {
    def sourcePath = Paths.get(sourceDir.toString(), fileName)
    def destPath = Paths.get(destDir.toString(), fileName)

    try {
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING)
        println "File copiato con successo da ${sourcePath} a ${destPath}"
    } catch (IOException e) {
        println "Errore durante la copia del file: ${e.message}"
    }
}

def copiaSovrascriviFile(sourceDir, destDir, fileNameExtra, fileName) {
    def sourcePath = Paths.get(sourceDir.toString(), fileNameExtra)
    def destPath = Paths.get(destDir.toString(), fileName)

    try {
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING)
        println "File copiato con successo da ${sourcePath} a ${destPath}"
    } catch (IOException e) {
        println "Errore durante la copia del file: ${e.message}"
    }
}

def creaCartella(cartella) {
    def path = Paths.get(cartella)

    try {
        Files.createDirectories(path)
        println "Cartella creata con successo: ${path}"
    } catch (IOException e) {
        println "Errore durante la creazione della cartella: ${e.message}"
    }
}

def trasformaPath(stringa) {
    return stringa.replace('.', '/')
}

println("---------------START POST PROCESS -------------")

Path projectPath = Paths.get(request.outputDirectory, request.artifactId)

Properties properties = request.properties

String packageName = properties.get("package")

String openapiflow = properties.get("openapiflow")

println("---projectPath: " + projectPath.toString())
println("---package: " + packageName)
println("---openapiflow: " + openapiflow)

println("inizializzazione API FIRST FLOW")

if (openapiflow == "hybrid"){

    println("flusso hybrid")
	copiaFile(projectPath.resolve("extra/resources"), projectPath.resolve("src/main/resources"), "application-docs.yaml")
	creaCartella(projectPath.toString() + "/src/main/java/"+trasformaPath(packageName)+"/config")
	copiaFile(projectPath.resolve("extra/class/openapi"), projectPath.resolve("src/main/java/"+trasformaPath(packageName)+"/config"), "OpenApiDefinitionConfig.java")
	creaCartella(projectPath.toString() + "/src/main/java/"+trasformaPath(packageName)+"/config/properties")
	copiaFile(projectPath.resolve("extra/class/openapi"), projectPath.resolve("src/main/java/"+trasformaPath(packageName)+"/config/properties"), "SwaggerProperties.java")
	
	println("cancella cartelle DOC")
    try {
        cancellaCartella(Paths.get(projectPath.toString() + "/doc"))
    } catch (IOException e) {
        println "Problemi nel cancellare /doc: ${e.message}"
    }

}else{

    println("flusso specification")
    copiaFile(projectPath.resolve("extra/resources"), projectPath.resolve("src/main/resources"), "application-docs.yaml")
	creaCartella(projectPath.toString() + "/src/main/java/"+trasformaPath(packageName)+"/config")
	copiaFile(projectPath.resolve("extra/class/openapi"), projectPath.resolve("src/main/java/"+trasformaPath(packageName)+"/config"), "OpenApiDefinitionConfig.java")
	creaCartella(projectPath.toString() + "/src/main/java/"+trasformaPath(packageName)+"/config/properties")
	copiaFile(projectPath.resolve("extra/class/openapi"), projectPath.resolve("src/main/java/"+trasformaPath(packageName)+"/config/properties"), "SwaggerProperties.java")
	
    copiaSovrascriviFile(projectPath.resolve("extra/class/openapi"), projectPath.resolve("src/main/java/"+trasformaPath(packageName)+"/controller"),"ProductController.java", "ProductController.java")

    copiaSovrascriviFile(projectPath.resolve("extra/poms"), projectPath.resolve("."),"pom-specification.xml", "pom.xml")
	copiaFile(projectPath.resolve("extra/resources"), projectPath.resolve("src/main/resources"), "application-docs.yaml")
}

println("cancella cartelle EXTRA")
try {
    cancellaCartella(Paths.get(projectPath.toString() + "/extra"))
} catch (IOException e) {
    println "Problemi nel cancellare /extra: ${e.message}"
}


println("---------------END POST PROCESS -------------")