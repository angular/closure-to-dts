declare namespace ಠ_ಠ.clutz {
  class module$exports$foo$C {
    private noStructuralTyping_: any;
    f (a : ಠ_ಠ.clutz.module$exports$foo$C.Enum ) : void ;
  }
}
declare namespace ಠ_ಠ.clutz.module$exports$foo$C {
  enum Enum {
    A = '' ,
  }
}
declare module 'goog:foo.C' {
  import C = ಠ_ಠ.clutz.module$exports$foo$C;
  export default C;
}
