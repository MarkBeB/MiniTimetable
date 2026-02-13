include "globals.mzn";
include "data/IHTC_model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%
include "constraints/H1.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%
include "constraints/H2.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%
include "constraints/H7.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%
%include "constraints/S1.mzn";


% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%
include "constraints/H8.mzn";


% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%
%include "constraints/S2.mzn";


% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%
%include "constraints/S3.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%
%include "constraints/S4.mzn";
/*
constraint forall(roomId in RoomIds, day in Days, shift in ShiftTypes)(
  roomVar[roomId].workload[day, shift] = 
    sum(occupant in occupantData where occupant.assignedRoom == roomId /\ day < occupant.lengthOfStay)
      (occupant.workloadProduced[enum2int(shift) - 1 + enum2int(day) * length(ShiftTypes)])
  + sum(patientId in PatientIds where patientVar[patientId].assignedRoom[day] == roomId)
      (patientData[patientId].workloadProduced[enum2int(shift) - 1 + (enum2int(day)-patientVar[patientId].admissionDay) * length(ShiftTypes)])   
);
*/


% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%
include "constraints/H3.mzn";


% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%
include "constraints/H4.mzn";


% SCP
% S5 Open OTs: The number of occupied OTs on each day should be minimized. Note that if an OT has no patients assigned for a particular day, it should not open on that day.
%
include "constraints/S5.mzn";


% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%
include "constraints/S6.mzn";


% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional pa-tients may be postponed to future periods.
%
include "constraints/H5.mzn";


% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%
include "constraints/H6.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%
include "constraints/S7.mzn";


% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%
include "constraints/S8.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Additional Constraints
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Derived from competition rules.
% H8 Room assignment: An admitted patient needs to be assigned to a room
%
constraint forall(id in PatientIds)(
  if patientVar[id].isAdmitted then
    sum(day in Days)(patientVar[id].assignedRoom[day] != <>) >= 1
  else
    forall(day in Days)(patientVar[id].assignedRoom[day] == <>)
  endif
);

/* no longer needed
constraint forall(patient in patientVar)(
  (patient.isAdmitted -> patient.assignedRoom = patient.assignedRoomAt[patient.admissionDay])
  /\ (not patient.isAdmitted -> patient.assignedRoom = <>)
);
*/

% Derived from competition rules.
% H9 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
%
constraint forall(patient in patientVar where patient.isAdmitted)(
  exists(roomId in RoomIds)(
    forall(day in Days)(
      if patient.admissionDay <= day /\ day <= patient.admittedUntil then
        patient.assignedRoom[day] = roomId
      else
        patient.assignedRoom[day] = <>
      endif
    )
  )
);

% Derived from competition rules.
% H10 Surgery on admission day: Patients undergo surgery on the day of their admission
%

% Every admitted patient must be assigned to an OT. Patients who have not been admitted should not be assigned to an OT.
constraint forall(patient in patientVar)(
  patient.isAdmitted -> patient.assignedOT != <>
  /\ not patient.isAdmitted -> patient.assignedOT == <>
);

%  The assigned OT should be available on the day of admission.
constraint forall(patient in patientVar where patient.isAdmitted)(
  exists(otId in OperatingTheaterIds where operatingTheaterData[otId].isAvailable[patient.admissionDay])(
    patient.assignedOT = otId
  )
);


%
% Performance constraints (Reduces search time to ~14seconds (before: 6+ minutes))
%

% Admissions should only be allowed on days when at least one OT is available.
set of ExtendedDays: daysWithOTCapacity = {postponedDay} union { day | day in Days where exists(ot in operatingTheaterData)(ot.isAvailable[day])};
constraint forall(patient in patientVar)(
  patient.admissionDay in daysWithOTCapacity
);

% Patients can only be admitted on days when their assigned surgeon is available.
constraint forall(patientId in PatientIds)( let {
    set of ExtendedDays: daysWithAvailableSurgeon = {postponedDay} union { day | day in Days where surgeonData[patientData[patientId].assignedSurgeon].maxSurgeryTime[day] > 0}
  } in
  patientVar[patientId].admissionDay in daysWithAvailableSurgeon
);





solve minimize score;
%solve :: int_search([Penalties.unscheduledOptional, Penalties.patientDelay, Penalties.roomMixedAge], input_order, indomain_median) minimize score;

include "OutConsoleFormat.mzn";
% include "model/OutJsonFormat.mzn";