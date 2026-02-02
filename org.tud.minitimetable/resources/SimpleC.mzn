include "model/IHTC_model.mzn";

%constraint forall(room in rooms, day in Day, patient in patients)(
%  patient.assignedRoom[day] == <>
%);

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%
include "model/constraints/H2.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

function bool: isRoom(var Patient: patient) = true;
constraint forall(patient in patients)(
  trace("patient \(patient)", isRoom(patient))
);

%constraint forall(room in rooms, day in Day)(
%  room.assignedPatients[day] == sum(patient in patients where isAdmittedToRoom(patient))(1) 
%  /\ room.assignedPatients[day] <= room.availableCapacity[day]
%);

function int: calc(int: a, int: b) = a + b;
function bool: notSame(int: a, int: b) = a != b;
constraint forall(a in 0..5)(100 >= sum(b in 0..5 where notSame(a,b))(calc(a,b)));

/*
constraint forall(room in rooms, day in Day)(
  room.assignedPatients[day] == sum(patient in patients where occurs(patient.assignedRoom[day]) /\ patient.assignedRoom[day] == room.id)(1) 
%  /\ room.assignedPatients[day] <= room.availableCapacity[day]
);
*/


% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%
include "model/constraints/S1.mzn";


% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%


% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%


% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%


% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%


% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

% SCP
% S5 Open OTs: The number of occupied OTs on each day should be minimized. Note that if an OT has no patients assigned for a particular day, it should not open on that day.
%

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%



% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional pa-tients may be postponed to future periods.
%

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%
include "model/constraints/H5-H6.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%
include "model/constraints/S7.mzn";

% Derived from competition rules.
% H8 Room assignment: An admitted patient needs to be assigned to a room
%

constraint forall(patient in patients)(
  patient.isAdmitted <-> patient.assignedToRoom != <>
);


% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%
include "model/constraints/S8.mzn";

% Derived from competition rules.
% H9 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
%


constraint forall(patient in patients where patient.isAdmitted)(
    forall(day in Day where patient.admissionDay <= day /\ day <= patient.admissionDay + patient.lengthOfStay)(
      patient.assignedRoom[day] == patient.assignedToRoom
    )
);


/*
constraint forall(patient in patients where patient.isAdmitted)(
  forall(day in Day where patient.admissionDay <= day /\ day < patient.admissionDay + patient.lengthOfStay)(
    if day == max(Day) then
      patient.assignedRoom[day] != <>
    else
      patient.assignedRoom[day] != <> /\ patient.assignedRoom[day] == patient.assignedRoom[enum_next(day)]
    endif
  )
);
*/


% Derived from competition rules.
% H10 Surgery on admission day: Patients undergo surgery on the day of their admission
%

solve minimize score;

include "model/OutConsoleFormat.mzn";
% include "model/OutJsonFormat.mzn";