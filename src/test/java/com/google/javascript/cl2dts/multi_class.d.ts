declare namespace ಠ_ಠ.cl2dts_internal.multi_class {
  class A {
    constructor (n : number ) ;
    a : number ;
  }
  class B extends A implements I , I2 {
    b : number ;
    noop ( ) : void ;
  }
  interface I {
  }
  interface I2 extends I {
  }
  class C extends B {
  }
  class D implements I {
  }
}
declare module 'goog:multi_class' {
  import alias = ಠ_ಠ.cl2dts_internal.multi_class;
  export = alias;
}
