// "Create abstract function 'bar'" "true"

interface I

class Foo : I {
    fun foo() {
        <caret>bar()
    }
}