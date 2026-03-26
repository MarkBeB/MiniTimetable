include "base/IHTP-model.mzn";
include "base/Utility.mzn";

include "base/constraints/Performance.mzn";
include "base/constraints/AllAH.mzn";
include "base/constraints/H1.mzn";
include "base/constraints/H2.mzn";
include "base/constraints/H3.mzn";
include "base/constraints/H4.mzn";
include "base/constraints/H5.mzn";
include "base/constraints/H6.mzn";
include "base/constraints/H7.mzn";
include "base/constraints/H8.mzn";

include "opt1/constraints/S1.mzn";
include "opt1/constraints/S2.mzn";
include "base/constraints/S3.mzn";
include "base/constraints/S4.mzn";
include "base/constraints/S5.mzn";
include "base/constraints/S6.mzn";
include "base/constraints/S7.mzn";
include "base/constraints/S8.mzn";

solve minimize score;
include "base/OutJsonFormat.mzn";
%include "OutConsoleFormat.mzn";