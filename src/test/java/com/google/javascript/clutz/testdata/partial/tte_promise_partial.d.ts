// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/tte_promise_partial.js
declare namespace ಠ_ಠ.clutz.module$exports$tte$Promise$Partial {
  export import PartialDeferred = ಠ_ಠ.clutz.module$contents$tte$Promise$Partial_PartialDeferred ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/tte_promise_partial.js
declare module 'goog:tte.Promise.Partial' {
  import Partial = ಠ_ಠ.clutz.module$exports$tte$Promise$Partial;
  export = Partial;
  const __clutz_actual_path: 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/tte_promise_partial';
}
declare module 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/tte_promise_partial' {
  import Partial = ಠ_ಠ.clutz.module$exports$tte$Promise$Partial;
  export = Partial;
  const __clutz_actual_namespace: 'tte.Promise.Partial';
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/tte_promise_partial.js
declare namespace ಠ_ಠ.clutz {
  class module$contents$tte$Promise$Partial_PartialDeferred < VALUE = any > extends ಠ_ಠ.clutz.Base < VALUE > {
    private noStructuralTyping_module$contents$tte$Promise$Partial_PartialDeferred : [ VALUE ];
    constructor ( ) ;
    then < RESULT > (opt_onFulfilled ? : ( (a : VALUE ) => PromiseLike < RESULT > | RESULT ) | null , opt_onRejected ? : ( (a : any ) => any ) | null) : ಠ_ಠ.clutz.module$contents$tte$Promise$Partial_PartialDeferred < RESULT > ;
  }
}
