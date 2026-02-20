include "data/IHTC_model.mzn";
include "constraints/AllAH.mzn";
include "constraints/Performance.mzn";
include "constraints/H1.mzn";
include "constraints/H2.mzn";
include "constraints/H3.mzn";
include "constraints/H4.mzn";
include "constraints/H5.mzn";
include "constraints/H6.mzn";
include "constraints/H7.mzn";
include "constraints/H8.mzn";

include "constraints/S7.mzn";
include "constraints/S8.mzn";

solve minimize score;
include "OutJsonFormat.mzn";