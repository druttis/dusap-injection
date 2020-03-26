# dusap-injection
The new dusap dependency injector

#### Supports
* Light-weight
* Small! (less than 30KB jar)
* ProviderMethod driven
* Hide/Expose Bindings
* Multiple annotation key qualifier
* Scoping
    * NO_SCOPING
    * SINGLETON
    * Register your own factory to extend with more scopes
* Generics supported
* Module with dependencies
* On the fly injector creation

#### To be done
* Injector.newInjector(Class<? extends Module> moduleType); // be able to create a child injector configured by specified module but that does not share the dependency graph. Good for plugins etc.

#### A little example

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
    List<String> provideStrings(@Named("a") String a, @Source(ModuleB.class) String b) {
        return Arrays.asList(a, b);
    }
    
    @Inject
    void dump(List<String> list) {
        System.out.println(list);
    }
}

class Test {
    public static void main(String[] args) {
        InjectionBuilder.newInjector(ModuleC.class);
    }
}
```

It is also possible to do this
```java
Injection injection = new InjectionBuilder()
    .withScopingFactory(MyScopeAnnotation.class, new MyScopingFactory())
    .build();
Injector injector = injection.getInjector(SomeModule.class);
```
