include "opt1/IHTP-model.mzn";
include "opt1/Utility.mzn";

include "opt1/constraints/Performance.mzn";
include "opt1/constraints/AllAH.mzn";
include "opt1/constraints/H1.mzn";
include "opt1/constraints/H2.mzn";
include "opt1/constraints/H3.mzn";
include "opt1/constraints/H4.mzn";
include "opt1/constraints/H5.mzn";
include "opt1/constraints/H6.mzn";
include "opt1/constraints/H7.mzn";
include "opt1/constraints/H8.mzn";

include "opt1/constraints/S1.mzn";
include "opt1/constraints/S2.mzn";
include "opt1/constraints/S3.mzn";
include "opt1/constraints/S4.mzn";
include "opt1/constraints/S5.mzn";
include "opt1/constraints/S6.mzn";
%include "opt1/constraints/S7.mzn";
%include "opt1/constraints/S8.mzn";

solve minimize score;
%include "opt1/OutJsonFormat.mzn";
%include "OutConsoleFormat.mzn";