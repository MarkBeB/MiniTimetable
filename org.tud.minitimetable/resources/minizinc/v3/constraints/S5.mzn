include "../IHTP-Model.mzn";

% SCP
% S5 Open OTs: The number of occupied OTs on each day should be minimized. Note that if an OT has no patients assigned for a particular day, it should not open on that day.
%

constraint (Penalties.openOperatingTheater = Weights.openOperatingTheater 
  * count(d in Days, ot in OperatingTheaterIds)(otTime[ot,d] > 0) 
) :: constraint_name("Con_S5_O");