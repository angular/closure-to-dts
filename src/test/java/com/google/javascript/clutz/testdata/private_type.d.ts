// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare namespace ಠ_ಠ.clutz.privatetype {
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare module 'goog:privatetype' {
  import privatetype = ಠ_ಠ.clutz.privatetype;
  export = privatetype;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare namespace ಠ_ಠ.clutz.privatetype {
  class Foo {
    private noStructuralTyping_privatetype_Foo : any;
    constructor (a ? : any ) ;
    foo ( ) : PrivateType ;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare namespace ಠ_ಠ.clutz.privatetype.Foo {
  type typedef = { a : PrivateType } ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare module 'goog:privatetype.Foo' {
  import Foo = ಠ_ಠ.clutz.privatetype.Foo;
  export default Foo;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare namespace ಠ_ಠ.clutz.privatetype {
  class X_ {
    private noStructuralTyping_privatetype_X_ : any;
    method ( ) : void ;
    static staticMethod ( ) : ಠ_ಠ.clutz.privatetype.X_ | null ;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare module 'goog:privatetype.X_' {
  import X_ = ಠ_ಠ.clutz.privatetype.X_;
  export default X_;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare namespace ಠ_ಠ.clutz.privatetype {
  let enumUser : PrivateType ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare module 'goog:privatetype.enumUser' {
  import enumUser = ಠ_ಠ.clutz.privatetype.enumUser;
  export default enumUser;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare namespace ಠ_ಠ.clutz.privatetype {
  let user : ಠ_ಠ.clutz.privatetype.X_ ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/private_type.js
declare module 'goog:privatetype.user' {
  import user = ಠ_ಠ.clutz.privatetype.user;
  export default user;
}
