declare namespace ಠ_ಠ.cl2dts_internal.multi_class {
  class A {
    a : number ;
  }
  class B extends A implements I, I2 {
    b : number ;
  }
  interface I {
  }
  interface I2 extends I {
  }
}
declare module 'goog:multi_class' {
  import alias = ಠ_ಠ.cl2dts_internal.multi_class;
  export = alias;
}
