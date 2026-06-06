include "../IHTP-Model.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%

% Bug: workload is not filtered by patient in room, no surprise that this is 'slightly' better thant he correcter v3 :D

array[NurseIds, Days, ShiftTypes] of var 0..: nurseExcessiveWorkload;

constraint forall(n in NurseIds, d in Days, s in ShiftTypes where n in activeNurses[d, s]) (
  sum(r in RoomIds) (
    isNurseInRoom[n, r, d, s] * (
      sum(p in PatientIds where r in compatibleRooms(p)) (
        sum(start in validAdmissionDays[p] where start <= d /\ d < start + patientLengthOfStay(p)) (
          (patientAdmissionDay[p] == start) * patientWorkload(p, d, s, start) %build constant coefficients for every possible admission day
        )
      )
      + sum(o in OccupantIds where isOccupantInRoom(o, d, r))(occupantWorkload(o,d,s))
    )
  ) <= nurseWorkloadCapacity(n,d,s) + nurseExcessiveWorkload[n,d,s]
  :: "Con_S4_A \(n) \(d) \(s)"
);

constraint penaltyNurseEccessiveWorkload = Weights.nurseEccessiveWorkload * (
  sum(n in NurseIds, d in Days, s in ShiftTypes)(nurseExcessiveWorkload[n,d,s])
) :: "Con_S4_O";