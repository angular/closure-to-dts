// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/required.js
declare namespace ಠ_ಠ.clutz.googmodule {
  class Required {
    private noStructuralTyping_googmodule_Required : any;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/required.js
declare module 'goog:googmodule.Required' {
  import Required = ಠ_ಠ.clutz.googmodule.Required;
  export default Required;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/goog_module.js
declare namespace ಠ_ಠ.clutz.module$exports$googmodule$TheModule {
  let a : number ;
  export import b = ಠ_ಠ.clutz.module$exports$googmodule$requiredModule.rm ;
  let required : ಠ_ಠ.clutz.googmodule.Required ;
  let requiredDefault : module$exports$googmodule$requiredModuleDefault ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/goog_module.js
declare module 'goog:googmodule.TheModule' {
  import TheModule = ಠ_ಠ.clutz.module$exports$googmodule$TheModule;
  export = TheModule;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/required_module.js
declare namespace ಠ_ಠ.clutz.module$exports$googmodule$requiredModule {
  let rm : number ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/required_module.js
declare module 'goog:googmodule.requiredModule' {
  import requiredModule = ಠ_ಠ.clutz.module$exports$googmodule$requiredModule;
  export = requiredModule;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/required_module_default.js
declare namespace ಠ_ಠ.clutz {
  class module$exports$googmodule$requiredModuleDefault {
    private noStructuralTyping_module$exports$googmodule$requiredModuleDefault : any;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/googModule/required_module_default.js
declare module 'goog:googmodule.requiredModuleDefault' {
  import requiredModuleDefault = ಠ_ಠ.clutz.module$exports$googmodule$requiredModuleDefault;
  export default requiredModuleDefault;
}
