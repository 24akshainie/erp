package domain;

import java.sql.Timestamp;

public class Notification {

    private int id;
    private int adminId;
    private String targetRole;
    private Integer targetUserId;
    private String message;
    private Timestamp createdAt;

    public int getId() { return id; }
    public int getAdminId() { return adminId; }
    public String getTargetRole() { return targetRole; }
    public Integer getTargetUserId() { return targetUserId; }
    public String getMessage() { return message; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
