// Generated from src/test/java/com/google/javascript/clutz/testdata/provide_instance.js
declare namespace ಠ_ಠ.clutz.provides {
  class C {
    private noStructuralTyping_provides_C : any;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/provide_instance.js
declare module 'goog:provides.C' {
  import C = ಠ_ಠ.clutz.provides.C;
  export default C;
  const __clutz_actual_path: 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/provide_instance';
}
declare module 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/provide_instance' {
  import C = ಠ_ಠ.clutz.provides.C;
  export { C };
  const __clutz_strip_property: 'C';
  const __clutz_actual_namespace: 'provides.C';
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/provide_instance.js
declare namespace ಠ_ಠ.clutz.provides {
  let instance : ಠ_ಠ.clutz.provides.C ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/provide_instance.js
declare module 'goog:provides.instance' {
  import instance = ಠ_ಠ.clutz.provides.instance;
  export default instance;
}
declare module 'google3/third_party/java_src/clutz/src/test/java/com/google/javascript/clutz/testdata/provide_instance' {
  export {};
  const __clutz_multiple_provides: true;
}
