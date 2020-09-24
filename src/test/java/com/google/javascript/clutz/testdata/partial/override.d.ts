// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/override.js
declare namespace ಠ_ಠ.clutz.module$exports$override {
  export import ExtendsBase = ಠ_ಠ.clutz.module$contents$override_ExtendsBase ;
  export import ExtendsInvisible = ಠ_ಠ.clutz.module$contents$override_ExtendsInvisible ;
  export import Template = ಠ_ಠ.clutz.module$contents$override_Template ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/override.js
declare module 'goog:override' {
  import override = ಠ_ಠ.clutz.module$exports$override;
  export = override;
  const __clutz_actual_path: 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/override';
}
declare module 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/override' {
  import override = ಠ_ಠ.clutz.module$exports$override;
  export = override;
  const __clutz_actual_namespace: 'override';
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/override.js
declare namespace ಠ_ಠ.clutz {
  class module$contents$override_ExtendsInvisible extends ಠ_ಠ.clutz.module$exports$override.Invisible {
    private noStructuralTyping_module$contents$override_ExtendsInvisible : any;
    constructor ( ) ;
    /**
     * This function has no known type, so its parameter should be optional.
     */
    inferredOverride (x ? : any ) : void ;
    /**
     * Ordinary function, for comparison with the others.
     */
    nonOverride (x : number ) : void ;
    /**
     * This function uses @override, but it includes type information, so that type should persist.
     */
    overrideWithType (x : number ) : number ;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/override.js
declare namespace ಠ_ಠ.clutz {
  class module$contents$override_ExtendsBase extends module$contents$override_Base {
    private noStructuralTyping_module$contents$override_ExtendsBase : any;
    /**
     * This function has no type information, but its base class is visible, so it should inherit
     * the types from the base.
     */
    method (x : number ) : void ;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/override.js
declare namespace ಠ_ಠ.clutz {
  interface module$contents$override_Template < T = any > {
    /**
     * The type of T in the callback should not be marked optional.
     */
    callbackWithTemplateArg < R = any > (f : (a : T ) => R ) : void ;
    /**
     * Note: we currently get this wrong, in that we mark the callback param as optional.
     * We can fix later if it matters.
     */
    callbackWithUnknownArg < R = any > (f : (a ? : any ) => R ) : void ;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/override.js
declare namespace ಠ_ಠ.clutz {
  class module$contents$override_Base {
    private noStructuralTyping_module$contents$override_Base : any;
    method (x : number ) : void ;
  }
}
