export class MyClass {
  type: InnerTypedefWithAssignment;
  /**
   * Constructor for MyClass
   */
  constructor(data: {type: InnerTypedefWithAssignment}) {
    /** @export type property*/
    this.type = data.type;
  }

  /**
   * equal function
   * @export
   */
  equals(otherData: MyClass): boolean {
    return this.type.a === otherData.type.a;
  }
}
export interface InnerTypedefWithAssignment {
  a: number;
  b: number;
}
export interface InnerTypedefNonNullable {
  a: number;
  b: number;
}
export interface InnerTypedefNullable {
  a: number;
  b: number;
}
export interface InnerTypedef {
  a: number;
}
export interface InnerTypedefWithNestedTypes {
  a: {b: {c: number}};
  d: string;
  e: (p1: string, p2: number) => number;
}
type InnerMyStringType = string;
type InnerMyStringTypeNonNullable = string;
type InnerMyStringTypeNullable = string|null;
type InnerMyAny1 = any;
type InnerMyAny2 = any;
type InnerUnionType = boolean|number|string;
type InnerNumberArrayType = number[];

interface Typedef {
  a: {b: {c: number}};
}
type MyStringType = string;

type MyStringTypeNonNullable = string;

type MyStringTypeNullable = string|null;

type MyAny = any;
