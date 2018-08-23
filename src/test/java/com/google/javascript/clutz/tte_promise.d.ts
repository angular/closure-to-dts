declare namespace ಠ_ಠ.clutz.angular.$q {
  class Promise < T > extends Promise_Instance < T > {
    static all(promises : ಠ_ಠ.clutz.angular.$q.Promise < any > [] ) : ಠ_ಠ.clutz.angular.$q.Promise < any [] > ;
    static race < T > (values : T [] ) : ಠ_ಠ.clutz.angular.$q.Promise < T > ;
    static resolve < T >(value: ಠ_ಠ.clutz.angular.$q.Promise < T > | T): ಠ_ಠ.clutz.angular.$q.Promise < T >;
  }
  class Promise_Instance < T > {
    private noStructuralTyping_: any;
    then < RESULT > (opt_onFulfilled ? : ( (a : T ) => ಠ_ಠ.clutz.angular.$q.Promise < RESULT > | RESULT | ಠ_ಠ.clutz.angular.$q.Promise<never>) | null , opt_onRejected ? : ( (a : any ) => any ) | null) : ಠ_ಠ.clutz.angular.$q.Promise < RESULT > ;
    when < RESULT, T > (value: T, successCallback: (promiseValue: T) => ಠ_ಠ.clutz.angular.$q.Promise < RESULT >|RESULT, errorCallback: null | undefined |  ((reason: any) => any), notifyCallback?: (state: any) => any): ಠ_ಠ.clutz.angular.$q.Promise < RESULT >;
  }
}
declare module 'goog:angular.$q.Promise' {
  import Promise = ಠ_ಠ.clutz.angular.$q.Promise;
  export default Promise;
}
declare namespace ಠ_ಠ.clutz.angular.$q {
  class PromiseService < T > extends PromiseService_Instance < T > {
  }
  class PromiseService_Instance < T > {
    private noStructuralTyping_: any;
    all(promises : ಠ_ಠ.clutz.angular.$q.PromiseService.Promise < any > [] ) : ಠ_ಠ.clutz.angular.$q.PromiseService.Promise < any [] > ;
  }
}
declare namespace ಠ_ಠ.clutz.angular.$q.PromiseService {
  interface Promise < T > {
  }
}
declare module 'goog:angular.$q.PromiseService' {
  import PromiseService = ಠ_ಠ.clutz.angular.$q.PromiseService;
  export default PromiseService;
}
