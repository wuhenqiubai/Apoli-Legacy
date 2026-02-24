import net.fabricmc.loom.task.RemapJarTask
import java.net.HttpURLConnection
import java.net.URI

plugins {
	id("fabric-loom") version "1.14-SNAPSHOT"
	`maven-publish`
}

base {
	archivesName.set(project.property("archives_base_name") as String)
}

version = "${project.property("mod_version")}+${project.property("minecraft_version")}"
group = project.property("maven_group") as String

repositories {
	maven("https://maven.ladysnake.org/releases") {
		name = "Ladysnake Mods"
	}
	maven("https://jitpack.io") {
		name = "JitPack"
	}
	maven("https://maven.cafeteria.dev") {
		content {
			includeGroup("net.adriantodt.fabricmc")
		}
	}
	maven("https://maven.shedaniel.me/")
	maven("https://maven.terraformersmc.com/")
	exclusiveContent {
		forRepository {
			maven("https://api.modrinth.com/maven") {
				name = "Modrinth"
			}
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
	mavenLocal()
}

loom {
	accessWidenerPath = file("src/main/resources/apoli.accesswidener")
	mods {
		register("apoli") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.test.get())
		}
	}
	runs {
		getByName("client") {
			client()
			source(sourceSets.test.get())
            configName = "Minecraft Client"
		}

		getByName("server") {
			server()
			source(sourceSets.test.get())
            configName = "Minecraft Server"
		}
	}
}

dependencies {
	implementation("com.demonwav.mcdev:annotations:1.0")

	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${project.property("parchment_version")}:${project.property("parchment_snapshot")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

	include(modApi("org.ladysnake.cardinal-components-api:cardinal-components-base:${project.property("cca_version")}") {
		exclude(group = "net.fabricmc.fabric-api")
	})

	include(modApi("org.ladysnake.cardinal-components-api:cardinal-components-entity:${project.property("cca_version")}") {
		exclude(group = "net.fabricmc.fabric-api")
	})

	include(modApi("io.github.ladysnake:PlayerAbilityLib:${property("pal_version")}")!!)

	implementation(project(":calio", "namedElements"))

	include(modApi("me.shedaniel.cloth:cloth-config-fabric:${project.property("clothconfig_version")}") {
		exclude(group = "net.fabricmc.fabric-api")
	})

	modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

	include(modApi("maven.modrinth:additionalentityattributes:${project.property("aea_version")}")!!)
	// modImplementation "de.dafuqs:AdditionalEntityAttributes:${project.aea_version}"
	// include "de.dafuqs:AdditionalEntityAttributes:${project.aea_version}"
}

project.tasks.getByName<RemapJarTask>("remapJar").nestedJars.from(project(":calio").tasks.getByName("remapJar"))

tasks.withType<JavaCompile>().configureEach {
	// ensure that the encoding is set to UTF-8, no matter what the system default is
	// this fixes some edge cases with special characters not displaying correctly
	// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
	// If Javadoc is generated, this must be specified in that task too.
	options.encoding = "UTF-8"

	options.release = 17
}

val targetJavaVersion = "17"

java {
	val javaVersion = JavaVersion.toVersion(targetJavaVersion)
	if (JavaVersion.current() < javaVersion) {
		toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
	}

	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

tasks {
	processResources {
		inputs.property("version", project.version)

		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}

	jar {
		from("LICENSE") {
			rename { "${it}_${project.base.archivesName.get()}" }
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.property("archives_base_name") as String
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		maven("https://mvn.devos.one/releases") {
			name = "devOS"
			credentials {
				username = System.getenv()["MAVEN_USER"]
				password = System.getenv()["MAVEN_PASS"]
			}
		}
	}
}

tasks.named("publishMavenJavaPublicationToDevOSRepository") {
	onlyIf {
		val group = project.property("maven_group") as String
		val artifactId = project.property("archives_base_name") as String
		val version = project.version.toString()

		try {
			val connection = URI.create("https://mvn.devos.one/releases/${group.replace(".", "/")}/${artifactId}/${version}/${artifactId}-${version}.jar").toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "GET"
			connection.connect()

			connection.responseCode != 200
		} catch (_: Exception) {
			false
		}
	}
}