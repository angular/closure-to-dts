declare namespace ಠ_ಠ.clutz.foo {
  type Bar = ಠ_ಠ.clutz.$jscomp.scope.Bar ;
  let Bar : typeof ಠ_ಠ.clutz.$jscomp.scope.Bar ;
  type Bar_Instance = ಠ_ಠ.clutz.$jscomp.scope.Bar_Instance ;
  let Bar_Instance : typeof ಠ_ಠ.clutz.$jscomp.scope.Bar_Instance ;
}
declare module 'goog:foo.Bar' {
  import Bar = ಠ_ಠ.clutz.foo.Bar;
  export default Bar;
}
declare namespace ಠ_ಠ.clutz.foo {
  type IBar = ಠ_ಠ.clutz.$jscomp.scope.IBar ;
}
declare module 'goog:foo.IBar' {
  import IBar = ಠ_ಠ.clutz.foo.IBar;
  export default IBar;
}
declare namespace ಠ_ಠ.clutz.foo {
  let boom : ಠ_ಠ.clutz.$jscomp.scope.Bar | null ;
}
declare module 'goog:foo.boom' {
  import boom = ಠ_ಠ.clutz.foo.boom;
  export default boom;
}
declare namespace ಠ_ಠ.clutz.foo {
  let iboom : ಠ_ಠ.clutz.$jscomp.scope.IBar | null ;
}
declare module 'goog:foo.iboom' {
  import iboom = ಠ_ಠ.clutz.foo.iboom;
  export default iboom;
}
declare namespace ಠ_ಠ.clutz.$jscomp.scope {
  class Bar extends Bar_Instance {
  }
  class Bar_Instance {
    private noStructuralTyping_: any;
  }
}
declare namespace ಠ_ಠ.clutz.$jscomp.scope {
  interface IBar {
  }
}
