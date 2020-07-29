package com.apps.emdad.models;

import java.io.Serializable;
import java.util.List;

public class UserModel implements Serializable {

    private User data;

    public User getData() {
        return data;
    }

    public void setData(User data) {
        this.data = data;
    }

    public static class User implements Serializable {
        private int id;
        private String name;
        private String email;
        private String city;
        private String phone_code;
        private String phone;
        private String image;
        private String logo;
        private String token;
        private String latitude;
        private String longitude;
        private String address;
        private String user_type;
        private String details;
        private List<Stage> stage_fk;
        private List<Stage_CLASS> class_fk;
        private List<SkillModel> skills_fk;
        private String fireBaseToken;

        public User() {
        }

        public User(int id, String name, String phone_code, String phone, String logo, String token) {
            this.id = id;
            this.name = name;
            this.phone_code = phone_code;
            this.phone = phone;
            this.logo = logo;
            this.token = token;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getCity() {
            return city;
        }

        public String getPhone_code() {
            return phone_code;
        }

        public String getPhone() {
            return phone;
        }

        public String getImage() {
            return image;
        }

        public String getLogo() {
            return logo;
        }

        public String getToken() {
            return token;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getAddress() {
            return address;
        }

        public String getType() {
            return user_type;
        }

        public String getFireBaseToken() {
            return fireBaseToken;
        }

        public void setFireBaseToken(String fireBaseToken) {
            this.fireBaseToken = fireBaseToken;
        }

        public List<SkillModel> getSkills_fk() {
            return skills_fk;
        }

        public String getDetails() {
            return details;
        }

        public List<Stage> getStage_fk() {
            return stage_fk;
        }


        public List<Stage_CLASS> getClass_fk() {
            return class_fk;
        }

        public static class Stage implements Serializable {
            private int id;
            private int stage_id;
            private StageClassName stage_class_name;

            public int getId() {
                return id;
            }

            public int getStage_id() {
                return stage_id;
            }

            public StageClassName getStage_class_name() {
                return stage_class_name;
            }

            public class StageClassName implements Serializable {
                private int id;
                private String title;

                public int getId() {
                    return id;
                }

                public String getTitle() {
                    return title;
                }
            }
        }

        public static class Stage_CLASS implements Serializable {
            private int id;
            private int class_id;
            private StageClassName stage_class_name;

            public int getId() {
                return id;
            }

            public int getClass_id() {
                return class_id;
            }

            public StageClassName getStage_class_name() {
                return stage_class_name;
            }

            public class StageClassName implements Serializable {
                private int id;
                private String title;

                public int getId() {
                    return id;
                }

                public String getTitle() {
                    return title;
                }
            }
        }


        public static class SkillModel implements Serializable{
            private int id;
            private String skill_type;

            public int getId() {
                return id;
            }

            public String getSkill_type() {
                return skill_type;
            }
        }
    }
}
