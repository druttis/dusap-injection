# dusap-injection
The new dusap dependency injector

* Light-weight
* Small! (less than 30KB jar)
* ProviderMethod driven
* Hide/Expose Bindings
* Scoping
* Generics

### Examples

```java
class ModuleA implements Module {
    // Binding will be visible to dependant module.
    @Provides
    @Expose
    String provideString() {
        return "A";
    }
    
    // Binding will only be visible to this module.
    @Provides
    @Named("n")
    int provideInteger() {
        1;
    }
    
    @Inject
    void dump(@Named("n") int num, String str) {
        System.out.println(num);
        System.out.println(str);
    }
}

class ModuleB implements Module {
    @Provides
    @Expose
    String provideString() {
        return "B";
    }
} 

@DependsOn({ModuleA.class, ModuleB.class})
class ModuleC implements Module {
    // some modules might want to expose dependency bindings.
    @Provides
    @Named("a")
    @Expose
    String provideString(@Source(ModuleA.class) String str) {
        return str;
    }

    // returns string bound to name 'a' and string bound in ModuleB as a string array
    @Provides
    @Singleton
    String[] provideStrings(@SNamed("a") String a, @Source(ModuleB.class) String b) {
        return new String[] {a, b};
    }
    
    @Inject
    void dump(String[] list) {
        System.out.println(Arrays.asList(list));
    }
}

class Test {
    public static void main(String[] args) {
        InjectionBuilder.newInjector(ModuleC.class);
    }
}
```