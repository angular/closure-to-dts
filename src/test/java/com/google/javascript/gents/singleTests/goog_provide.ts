goog.require('RequiredType');

export class B {
  constructor(public n: number) {}
}

// Aggressively export rather than create static methods/fields
export function foo(): number {
  return 4;
}

export const num: number = 8;

export class C {}

export function bar(): boolean {
  return false;
}

export const D = path.to.someUtilFunction();
D.setA(1).setB(2);
