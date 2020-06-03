// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  enum EscapedEnum {
    A = '\\' ,
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.EscapedEnum' {
  import EscapedEnum = ಠ_ಠ.clutz.some.EscapedEnum;
  export default EscapedEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  type MixedEnum = ( string | number | boolean ) &{clutzEnumBrand: never} ;
  let MixedEnum : {
    A : MixedEnum ,
    B : MixedEnum ,
    C : MixedEnum ,
  };
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.MixedEnum' {
  import MixedEnum = ಠ_ಠ.clutz.some.MixedEnum;
  export default MixedEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  type NumberAsKey = ( string ) &{clutzEnumBrand: never} |'a' |'b' ;
  let NumberAsKey : {
    1 : 'a' ,
    2 : 'b' ,
  };
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.NumberAsKey' {
  import NumberAsKey = ಠ_ಠ.clutz.some.NumberAsKey;
  export default NumberAsKey;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  type ObjectValuedEnum = ( X ) &{clutzEnumBrand: never} ;
  let ObjectValuedEnum : {
    A : ObjectValuedEnum ,
    B : ObjectValuedEnum ,
  };
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.ObjectValuedEnum' {
  import ObjectValuedEnum = ಠ_ಠ.clutz.some.ObjectValuedEnum;
  export default ObjectValuedEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  type PartialLiteralStringEnum = ( string ) &{clutzEnumBrand: never} |'1' |'2' ;
  let PartialLiteralStringEnum : {
    A : '1' ,
    B : PartialLiteralStringEnum ,
    C : '2' ,
    D : PartialLiteralStringEnum ,
  };
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.PartialLiteralStringEnum' {
  import PartialLiteralStringEnum = ಠ_ಠ.clutz.some.PartialLiteralStringEnum;
  export default PartialLiteralStringEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  enum SomeEnum {
    A = 1.0 ,
    B = 2.0 ,
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.SomeEnum' {
  import SomeEnum = ಠ_ಠ.clutz.some.SomeEnum;
  export default SomeEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  enum StringEnum {
    A = '1' ,
    B = '2' ,
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.StringEnum' {
  import StringEnum = ಠ_ಠ.clutz.some.StringEnum;
  export default StringEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  type StringVariableEnum = ( string ) &{clutzEnumBrand: never} ;
  let StringVariableEnum : {
    A : StringVariableEnum ,
    B : StringVariableEnum ,
  };
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.StringVariableEnum' {
  import StringVariableEnum = ಠ_ಠ.clutz.some.StringVariableEnum;
  export default StringVariableEnum;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz.some {
  function setEnvironment (a : Environment ) : any ;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare module 'goog:some.setEnvironment' {
  import setEnvironment = ಠ_ಠ.clutz.some.setEnvironment;
  export default setEnvironment;
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz {
  class X {
    private noStructuralTyping_X : any;
  }
}
// Generated from src/test/java/com/google/javascript/clutz/testdata/enum.js
declare namespace ಠ_ಠ.clutz {
  enum Environment {
    FAKE = 0.0 ,
    PROD = 4.0 ,
  }
}
