# Create Admin User Script
# Run this in PowerShell or use Postman

# Create Admin User
$adminUser = @{
    name = "admin"
    email = "admin@codeforchallenge.com"
    password = "admin123"
    roles = @("ADMIN", "USER")
    profileImg = ""
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9090/Public/Create-User" `
    -Method POST `
    -ContentType "application/json" `
    -Body $adminUser

Write-Host "Admin user created successfully!" -ForegroundColor Green
Write-Host "Username: admin" -ForegroundColor Cyan
Write-Host "Password: admin123" -ForegroundColor Cyan
Write-Host ""
Write-Host "To login, make a POST request to:" -ForegroundColor Yellow
Write-Host "http://localhost:9090/users/login" -ForegroundColor Yellow
Write-Host ""
Write-Host "Body:" -ForegroundColor Yellow
Write-Host '{
  "name": "admin",
  "password": "admin123"
}' -ForegroundColor White
