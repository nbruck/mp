package com.motorpast.additional;


public enum MotorRequestState
{
    SimpleViewRequest,                                      // the most simple case - ask for showing data
    SaveCompleteNewCarTupel,                                // is the first access - storing id + mileage
    UpdateCarTupelWhichDoesntAlreadyHaveRegistrationDate,   // update existing cartupel and mileages_tbl (cartupel has no regdate)
    UpdateCarTupelWithAttemptsLeft,                         // cartupel has regdate and attempts left (not 0)
    CarTupelNoAttemptsLeftAndBlocked,                        // regdate exists but 0 attepts

    ;
}
