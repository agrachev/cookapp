import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

typealias App = ApplicationExtension
typealias Lib = LibraryExtension

inline fun <reified T : Any> Project.android(noinline action: T.() -> Unit) =
    this.extensions.configure<T>(action)
