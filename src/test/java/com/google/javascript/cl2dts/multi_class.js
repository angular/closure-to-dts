goog.provide('multi_class');

/** @constructor */
multi_class.A = function() {
  /** @type {number} */
  this.a;
};

/**
 * @constructor
 * @extends {multi_class.A}
 * @implements {multi_class.I}
 * @implements {multi_class.I2}
 */
multi_class.B = function() {
  /** @type {number} */
  this.b;
};

/** @interface */
multi_class.I = function() {};

/**
 * @interface
 * @extends {multi_class.I}
 */
multi_class.I2 = function() {};
