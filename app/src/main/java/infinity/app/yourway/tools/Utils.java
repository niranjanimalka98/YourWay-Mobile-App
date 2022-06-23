package infinity.app.yourway.tools;

import java.util.List;

import infinity.app.yourway.Model.PassengerModel;

public class Utils {

    public static final PassengerModel get_logged_in_user() {
        try {
            List<PassengerModel> allUsers = PassengerModel.listAll(PassengerModel.class);
            if(allUsers == null){
                return null;
            }
            if (allUsers.isEmpty()){
                return null;
            }
            return allUsers.get(0);

        }catch (Exception e){
            return null;
        }

    }
}
