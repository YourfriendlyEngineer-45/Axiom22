# Create .gitignore
cat > .gitignore << 'EOF'
build/
out/
target/
*.class
*.jar
.idea/
*.iml
.vscode/
.gradle/
.DS_Store
.env
*.key
*.pem
EOF

# Remove any accidentally tracked files
git rm -r --cached .
git add .
git commit -m "chore: Add .gitignore for enterprise-grade repo hygiene"
git push
