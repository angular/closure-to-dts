declare namespace ಠ_ಠ.clutz.privatetype {
}
declare module 'goog:privatetype' {
  import alias = ಠ_ಠ.clutz.privatetype;
  export = alias;
}
declare namespace ಠ_ಠ.clutz.privatetype {
  class Foo extends Foo_Instance {
  }
  class Foo_Instance {
    private noStructuralTyping_: any;
    constructor (a : any ) ;
    foo ( ) : ಠ_ಠ.clutz.PrivateType ;
  }
}
declare namespace ಠ_ಠ.clutz.privatetype.Foo {
  type typedef = { a : ಠ_ಠ.clutz.PrivateType } ;
}
declare namespace ಠ_ಠ.clutz.goog {
  function require(name: 'privatetype.Foo'): typeof ಠ_ಠ.clutz.privatetype.Foo;
}
declare module 'goog:privatetype.Foo' {
  import alias = ಠ_ಠ.clutz.privatetype.Foo;
  export default alias;
}
declare namespace ಠ_ಠ.clutz.privatetype {
  class X_ extends X__Instance {
    static staticMethod ( ) : void ;
  }
  class X__Instance {
    private noStructuralTyping_: any;
    method ( ) : void ;
  }
}
declare namespace ಠ_ಠ.clutz.goog {
  function require(name: 'privatetype.X_'): typeof ಠ_ಠ.clutz.privatetype.X_;
}
declare module 'goog:privatetype.X_' {
  import alias = ಠ_ಠ.clutz.privatetype.X_;
  export default alias;
}
declare namespace ಠ_ಠ.clutz.privatetype {
  var enumUser : ಠ_ಠ.clutz.PrivateType ;
}
declare namespace ಠ_ಠ.clutz.goog {
  function require(name: 'privatetype.enumUser'): typeof ಠ_ಠ.clutz.privatetype.enumUser;
}
declare module 'goog:privatetype.enumUser' {
  import alias = ಠ_ಠ.clutz.privatetype.enumUser;
  export default alias;
}
declare namespace ಠ_ಠ.clutz.privatetype {
  var user : ಠ_ಠ.clutz.privatetype.X_ ;
}
declare namespace ಠ_ಠ.clutz.goog {
  function require(name: 'privatetype.user'): typeof ಠ_ಠ.clutz.privatetype.user;
}
declare module 'goog:privatetype.user' {
  import alias = ಠ_ಠ.clutz.privatetype.user;
  export default alias;
}
