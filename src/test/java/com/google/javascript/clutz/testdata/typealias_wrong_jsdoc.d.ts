// Generated from src/test/java/com/google/javascript/clutz/testdata/typealias_wrong_jsdoc.js
declare namespace ಠ_ಠ.clutz.typedef {
  class C {
    private noStructuralTyping_typedef_C : any;
    /**
     * These types should *not* be emitted as PrivateType as typedef.C.T is public.
     * If clutz picks up the jsdoc from "this.privateUsage", as closure incorrectly
     * assigns it, they will emit as PrivateType.
     */
    f (a : ಠ_ಠ.clutz.typedef.C.T , b ? : ಠ_ಠ.clutz.typedef.C.T , c ? : ( ಠ_ಠ.clutz.typedef.C.T ) | null ) : void ;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/typealias_wrong_jsdoc.js
declare namespace ಠ_ಠ.clutz.typedef.C {
  type T = (a ? : any ) => any ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/typealias_wrong_jsdoc.js
declare module 'goog:typedef.C' {
  import C = ಠ_ಠ.clutz.typedef.C;
  export default C;
}
