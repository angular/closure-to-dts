// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/export_local_prop.js
declare namespace ಠ_ಠ.clutz.module$exports$exports$local$prop {
  //!! This is a bug. ns.a doesn't exist as it is local.
  //!! Should be either ಠ_ಠ.clutz.NsType.a (or some module variant).
  export import a = ಠ_ಠ.clutz.ns.a ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/partial/export_local_prop.js
declare module 'goog:exports.local.prop' {
  import prop = ಠ_ಠ.clutz.module$exports$exports$local$prop;
  export = prop;
  const __clutz_actual_path: 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/export_local_prop';
}
declare module 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/partial/export_local_prop' {
  import prop = ಠ_ಠ.clutz.module$exports$exports$local$prop;
  export = prop;
  const __clutz_actual_namespace: 'exports.local.prop';
}
