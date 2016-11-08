var nop = function() {};

// Function params
/**
 * @param {number} n
 */
var oneParam = function(n) {};

/**
 * @param {boolean} b
 * @param {string} s
 */
function twoParams(b, s) {}

/** @param {!Array<?>=} list */
function withDefaultValue(list = []) {}

// Function returns
/**
 * @return {*}
 */
var anyReturn = function() { return "hello"; };

/**
 * @return {number}
 */
function typedReturn() { return 4; }

/**
 * @param {number} n
 * @param {boolean} b
 */
var partiallyTyped = function(n, u1, b, u2) {};

// Both params and returns
/**
 * @param {boolean} b
 * @param {string} s
 * @param {?} x
 * @return {string}
 */
var complex = function(b, s, x) { if (b) { return s; }};

// Undefined params

/**
 * @param {undefined} u
 * @param {void} v
 */
var paramUndef = function(u, v) {};

// Void returns

/**
 * @return {void}
 */
var retVoid = function() {};

/**
 * @return {undefined}
 */
var retUndef = function() {};

/**
 * @param {number} a
 * @return {number}
 */
const arrowWithJsDoc = a => { return a; };

const arrowNoJsDoc = a => { return a; };

const implicitReturnArrow = a => a;
