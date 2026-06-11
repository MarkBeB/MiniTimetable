enum Patients = {p1, p2, p3, p4, p5, p6, p7};
enum Rooms = {r1, r2};
set of int: Days = 1..7;
int: stayLength = 2;
int: roomCapacity = 2;

array[Patients, Rooms] of var bool: isPatientInRoom;
array[Patients, Days] of var bool: isPatientAdmittedAt;
array[Rooms, Days] of var bool: isOccupiedAt;

constraint forall(p in Patients)(
 sum(d in Days)(isPatientAdmittedAt[p,d]) <= 1
 :: "Admitted once \(p)"
);

constraint forall(p in Patients)(
 sum(r in Rooms)(isPatientInRoom[p,r]) = sum(d in Days)(isPatientAdmittedAt[p,d])
 :: "One Room \(p)"
);

constraint forall(r in Rooms, d in Days)(
 roomCapacity >= sum(p in Patients)(isInRoomAt(p,r,d))
);

constraint forall(r in Rooms, d in Days)(
 isOccupiedAt[r,d] == exists(p in Patients)(isInRoomAt(p,r,d))
);

function var bool: isInRoomAt(Patients: p, Rooms: r, Days: d) = isInRoomAt[p,r,d];
predicate isInRoom(Patients: p, Rooms: r) = isPatientInRoom[p,r];
predicate isPresent(Patients: p, Days: d) = isPresent[p,d];

array[Patients, Days] of var bool: isPresent = [(p,d) :
 exists(b in Days)(isPatientAdmittedAt[p,b] /\ b <= d /\ d <= b + stayLength)
 | p in Patients, d in Days
];
 
array[Patients, Rooms, Days] of var bool: isInRoomAt = [(p,r,d):
  isPresent(p,d) /\ isInRoom(p,r)
  | p in Patients, r in Rooms, d in Days
];

solve minimize sum(r in Rooms, d in Days)(isOccupiedAt[r,d]);




%predicate isPatientPresent(Patients: p, Days: b, Days: d) = isPatientAdmittedAt[p,b] /\ b <= d /\ d <= b + stayLength;

%predicate isPatientPresent(Patients: p, Days: d) = exists(b in Days)(isPatientAdmittedAt[p,b] /\ b <= d /\ d <= b + stayLength);

%predicate isPatientPresent2(Patients: p, Days: d) = sum(b in Days)(isPatientPresent(p,b,d)) == 1;


%array[Patients, Days] of var bool: isPatientPresent;
%constraint forall(p in Patients, d in Days) (
%  (isPatientPresent[p, d] <-> exists(b in Days)(isPatientAdmittedAt[p,b] /\ b <= d /\ d <= b + stayLength)) 
%  :: "Var isPatientPresent \(p) \(d)"
%);




