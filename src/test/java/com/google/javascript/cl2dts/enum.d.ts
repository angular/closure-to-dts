declare namespace ಠ_ಠ.cl2dts_internal.SomeEnum {
  type SomeEnum = number ;
  var SomeEnum : {
    A : SomeEnum ,
    B : SomeEnum ,
  };
}
declare module 'goog:SomeEnum' {
  import alias = ಠ_ಠ.cl2dts_internal.SomeEnum;
  export = alias;
}
