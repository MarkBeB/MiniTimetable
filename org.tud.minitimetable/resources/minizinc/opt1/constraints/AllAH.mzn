include "../IHTP-model.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Additional Constraints
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Derived from competition rules.
% AH9 Room assignment: An admitted patient needs to be assigned to a room
%

constraint forall(p in PatientIds, d in Days)(
    sum(r in RoomIds)(isPatientInRoom[p, d, r]) = isPatientPresent(p, d)
) :: constraint_name("Con_AH9_A");

% Derived from competition rules.
% AH10 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
%

constraint forall(p in PatientIds, d in Days, r in RoomIds)(
    isPatientInRoom[p, d, r] = (isPatientPresent(p, d) /\ patientRoomAssignment[p] == r)
) :: constraint_name("Con_AH10");


% Derived from competition rules.
% AH11 Surgery on admission day: Patients undergo surgery on the day of their admission
%

% Every admitted patient must be assigned to an OT. Patients who have not been admitted should not be assigned to an OT.
constraint forall(p in PatientIds)(
  isPatientAdmitted(p) -> patientOTAssignment[p] != <>
  /\ isPatientPostponed(p) -> patientOTAssignment[p] == <>
) :: constraint_name("Con_AH11_A");

%  The assigned OT should be available on the day of admission.
constraint forall(p in PatientIds where isPatientAdmitted(p))(
  exists(ot in OperatingTheaterIds where operatingTheaters[ot].availability[patientAdmissionDay[p]] > 0)(
    patientOTAssignment[p] = ot
  )
) :: constraint_name("Con_AH11_B");


