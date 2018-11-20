declare namespace ಠ_ಠ.clutz.generics {
  interface ExtendGenericInterface < TYPE > extends ಠ_ಠ.clutz.generics.GenericInterface < TYPE > {
  }
  class ExtendsGenericClass < TYPE > extends ExtendsGenericClass_Instance < TYPE > {
  }
  class ExtendsGenericClass_Instance < TYPE > extends ಠ_ಠ.clutz.generics.Foo_Instance < TYPE , number > {
    constructor ( ) ;
  }
  class Foo < T , U > extends Foo_Instance < T , U > {
  }
  class Foo_Instance < T , U > {
    private noStructuralTyping_: any;
    constructor (a : number ) ;
    get ( ) : T ;
    loop < V , W > (t : T , v : V ) : any ;
    set (t : T ) : void ;
  }
  interface GenericInterface < TYPE > {
  }
  class ImplementsGenericInterface < TYPE > extends ImplementsGenericInterface_Instance < TYPE > {
  }
  class ImplementsGenericInterface_Instance < TYPE > implements ಠ_ಠ.clutz.generics.GenericInterface < TYPE > {
    private noStructuralTyping_: any;
  }
  let arrayMissingTypeParam : any [] ;
  let fooMissingAllTypeParams : ಠ_ಠ.clutz.generics.Foo < any , any > ;
  let fooMissingOneTypeParam : ಠ_ಠ.clutz.generics.Foo < string , any > ;
  function genericFunction < T > (a : T ) : T ;
  function identity < T > (a : T ) : T ;
  function objectWithGenericKeyType < K , V > (obj : { [ /* warning: coerced from K */ key: string ]: V } ) : void ;
}
declare module 'goog:generics' {
  import generics = ಠ_ಠ.clutz.generics;
  export = generics;
}
