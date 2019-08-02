goog.module("missing.imported.base");

const MissingGoogRequire = goog.require('default.base.exporter');
/**
 * @constructor
 * @extends {MissingGoogRequire}
 */
function ClassExtendingMissingRequire() {

}

/**
 * @param {MissingGoogRequire} c
 */
function FuncWithMissingRequireParam(c) {

}

/** @type {MissingGoogRequire} */
const DeclarationOfMissingRequire = null;

const {MissingDestructuredRequire, OriginalName: RenamedDestructuredRequire} = goog.require('named.base.exporter');

/**
 * @constructor
 * @extends {MissingDestructuredRequire}
 */
function ClassExtendingMissingDestructuredRequire() {

}

/**
 * @constructor
 * @extends {RenamedDestructuredRequire}
 */
function ClassExtendingRenamedDestructuredRequire() {

}

const DefaultObjectExporter = goog.require('default.object.exporter');

/**
 * @constructor
 * @extends {DefaultObjectExporter.BaseClass}
 */
function ClassExtendingDefaultObjectExporterBaseClass() {

}

exports.ClassExtendingMissingRequire = ClassExtendingMissingRequire;
exports.FuncWithMissingRequireParam = FuncWithMissingRequireParam;
exports.DeclarationOfMissingRequire = DeclarationOfMissingRequire;
exports.ClassExtendingMissingDestructuredRequire = ClassExtendingMissingDestructuredRequire;
exports.ClassExtendingRenamedDestructuredRequire = ClassExtendingRenamedDestructuredRequire;
exports.ClassExtendingDefaultObjectExporterBaseClass = ClassExtendingDefaultObjectExporterBaseClass;
