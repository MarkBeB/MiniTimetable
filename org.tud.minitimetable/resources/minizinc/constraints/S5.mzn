include "../data/IHTC_model.mzn";

% SCP
% S5 Open OTs: The number of occupied OTs on each day should be minimized. Note that if an OT has no patients assigned for a particular day, it should not open on that day.
%

constraint Penalties.openOperatingTheater = Weights.openOperatingTheater 
  * count(day in Days, ot in operatingTheaterVar)(ot.usedTime[day] > 0);