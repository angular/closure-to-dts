///<reference path="./types"/>
import * as t from 'goog:types';
t.g(1, "something");

function thing({a, b}: {a: string, b: number}) {
  console.log(a, 1 + b);
}
thing(t.h);
