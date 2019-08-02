import Promise from 'goog:angular.$q.Promise';

let x = new Promise<string>();
let y: Promise<string> = x.then(x => x).then(x => x + 'foo');
let z: Promise<boolean> = x.then(x => new Promise<boolean>());
let w: Promise<number> = x.then(x => 0);

let all: Promise<any[]> = Promise.all([x, y, z, w]);