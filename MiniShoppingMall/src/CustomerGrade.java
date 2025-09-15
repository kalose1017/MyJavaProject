import java.sql.*;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 고객 등급 계산 및 관리
 * - 누적 충전액에 따른 등급 자동 업데이트
 * - 등급별 기준 관리
 * 
 * 등급 기준:
 * - Bronze: 기본 등급 (0원 이상)
 * - Silver: 50만원 이상
 * - Gold: 150만원 이상  
 * - Diamond: 300만원 이상
 * - VIP: 500만원 이상
 * 
 * 포함된 메소드:
 * - calculateGrade(): 누적 충전액에 따른 등급 계산
 * - updateCustomerGrade(): 고객 등급 업데이트
 * - getGradeInfo(): 등급 정보 조회
 * 
 * ===========================================
 */
public class CustomerGrade {
    
    // 등급 기준 상수
    private static final int SILVER_THRESHOLD = 500000;  // 50만원
    private static final int GOLD_THRESHOLD = 1500000;   // 150만원
    private static final int DIAMOND_THRESHOLD = 3000000; // 300만원
    private static final int VIP_THRESHOLD = 5000000;     // 500만원
    
    /**
     * 누적 충전액에 따른 등급을 계산합니다.
     * @param totalCharge 누적 충전액
     * @return 계산된 등급
     */
    public static String calculateGrade(double totalCharge) {
        if (totalCharge >= VIP_THRESHOLD) {
            return "VIP";
        } else if (totalCharge >= DIAMOND_THRESHOLD) {
            return "Diamond";
        } else if (totalCharge >= GOLD_THRESHOLD) {
            return "Gold";
        } else if (totalCharge >= SILVER_THRESHOLD) {
            return "Silver";
        } else {
            return "Bronze";
        }
    }
    
    /**
     * 고객의 등급을 업데이트합니다.
     * @param customerId 고객 ID
     * @param loginId 로그인 ID
     * @param nickName 닉네임
     * @return 등급 업데이트 성공 여부
     */
    public static boolean updateCustomerGrade(int customerId, String loginId, String nickName) {
        String sql = "UPDATE Customer SET Grade = ? WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
        
        try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 현재 누적 충전액 조회
            String totalChargeSql = "SELECT TotalCharge FROM Customer WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
            try (PreparedStatement totalChargePstmt = conn.prepareStatement(totalChargeSql)) {
                totalChargePstmt.setInt(1, customerId);
                totalChargePstmt.setString(2, loginId);
                totalChargePstmt.setString(3, nickName);
                
                ResultSet rs = totalChargePstmt.executeQuery();
                if (rs.next()) {
                    double totalCharge = rs.getDouble("TotalCharge");
                    String newGrade = calculateGrade(totalCharge);
                    
                    // 등급 업데이트
                    pstmt.setString(1, newGrade);
                    pstmt.setInt(2, customerId);
                    pstmt.setString(3, loginId);
                    pstmt.setString(4, nickName);
                    
                    int result = pstmt.executeUpdate();
                    return result > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("\n등급 업데이트 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 고객의 등급 정보를 조회합니다.
     * @param customerId 고객 ID
     * @param loginId 로그인 ID
     * @param nickName 닉네임
     * @return 등급 정보 (등급, 누적 충전액, 다음 등급까지 필요한 금액)
     */
    public static String getGradeInfo(int customerId, String loginId, String nickName) {
        String sql = "SELECT Grade, TotalCharge FROM Customer WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
        
        try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            pstmt.setString(2, loginId);
            pstmt.setString(3, nickName);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String currentGrade = rs.getString("Grade");
                double totalCharge = rs.getDouble("TotalCharge");
                
                // 다음 등급까지 필요한 금액 계산
                int nextThreshold = getNextThreshold(currentGrade);
                int remainingAmount = nextThreshold - (int)totalCharge;
                
                StringBuilder gradeInfo = new StringBuilder();
                gradeInfo.append("현재 등급: ").append(currentGrade).append("\n");
                gradeInfo.append("누적 충전액: ").append((int)totalCharge).append("원\n");
                
                if (remainingAmount > 0) {
                    gradeInfo.append("다음 등급까지: ").append(remainingAmount).append("원");
                } else {
                    gradeInfo.append("최고 등급입니다!");
                }
                
                return gradeInfo.toString();
            }
        } catch (SQLException e) {
            System.out.println("\n등급 정보 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        return "등급 정보를 조회할 수 없습니다.";
    }
    
    /**
     * 현재 등급의 다음 등급까지 필요한 금액을 계산합니다.
     * @param currentGrade 현재 등급
     * @return 다음 등급까지 필요한 금액
     */
    private static int getNextThreshold(String currentGrade) {
        switch (currentGrade) {
            case "Bronze":
                return SILVER_THRESHOLD;
            case "Silver":
                return GOLD_THRESHOLD;
            case "Gold":
                return DIAMOND_THRESHOLD;
            case "Diamond":
                return VIP_THRESHOLD;
            case "VIP":
                return VIP_THRESHOLD; // 최고 등급
            default:
                return SILVER_THRESHOLD;
        }
    }
    
    /**
     * 등급별 혜택 정보를 반환합니다.
     * @return 등급별 혜택 정보
     */
    public static String getGradeBenefits() {
        StringBuilder benefits = new StringBuilder();
        benefits.append("=== 등급 소개 ===\n");
        benefits.append("Bronze: 기본 등급\n");
        benefits.append("Silver: 50만원 이상 충전\n");
        benefits.append("Gold: 150만원 이상 충전\n");
        benefits.append("Diamond: 300만원 이상 충전\n");
        benefits.append("VIP: 500만원 이상 충전\n");
        benefits.append("==================");
        
        return benefits.toString();
    }
}
