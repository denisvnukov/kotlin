// "Create abstract function 'bar'" "true"

abstract class A

class Foo : A() {
    fun foo() {
        <caret>bar()
    }
}