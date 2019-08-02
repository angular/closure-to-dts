declare namespace ಠ_ಠ.clutz.ctor_func {
  class Ctor < T > {
    private noStructuralTyping_ctor_func_Ctor : any;
    constructor (a : string , b : number ) ;
  }
  let ctorFuncField : { new (a : string , b : number ) : ಠ_ಠ.clutz.ctor_func.Ctor < any > } ;
  let ctorFuncFieldAlias : { new (a : string , b : number ) : ಠ_ಠ.clutz.ctor_func.Ctor < any > } ;
  function ctorFuncParam (ctor : { new (a : number ) : ಠ_ಠ.clutz.ctor_func.Ctor < any > } ) : void ;
  function ctorFuncParamTemplatized < T > (ctor : { new (a : number ) : T } ) : T ;
}
declare module 'goog:ctor_func' {
  import ctor_func = ಠ_ಠ.clutz.ctor_func;
  export = ctor_func;
}
