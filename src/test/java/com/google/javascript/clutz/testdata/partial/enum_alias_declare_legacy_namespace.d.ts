// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/enum_alias_declare_legacy_namespace.js
declare namespace ಠ_ಠ.clutz.enum_alias2 {
  //!! This is wrong -- we lose that Enum is a type.
  //!! This is a combination of the form of the exports in the test and
  //!! the use of declareLegacyNamespace.
  let Enum : any ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/enum_alias_declare_legacy_namespace.js
declare module 'goog:enum_alias2' {
  import enum_alias2 = ಠ_ಠ.clutz.enum_alias2;
  export = enum_alias2;
  const __clutz_actual_path: 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/enum_alias_declare_legacy_namespace';
}
declare module 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/enum_alias_declare_legacy_namespace' {
  import enum_alias2 = ಠ_ಠ.clutz.enum_alias2;
  export = enum_alias2;
  const __clutz_actual_namespace: 'enum_alias2';
}
