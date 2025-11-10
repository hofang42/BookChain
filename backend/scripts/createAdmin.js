const mongoose = require("mongoose");
const dotenv = require("dotenv");
const User = require("../src/models/User");

// Load environment variables
dotenv.config({ path: './.env' });

const createAdmin = async () => {
  try {
    // Connect to MongoDB
    const uri = "mongodb+srv://trongducdoan25:Tduc123@ducevent.xowgeoc.mongodb.net/BookChain?retryWrites=true&w=majority&appName=BookChain";
    await mongoose.connect(uri, {
      autoIndex: true,
      maxPoolSize: 10,
      serverSelectionTimeoutMS: 5000,
    });
    console.log("✓ MongoDB connected");

    // Check if admin already exists
    const existingAdmin = await User.findOne({
      $or: [
        { username: "admin" },
        { email: "admin@bookchain.com" }
      ]
    });

    if (existingAdmin) {
      console.log("⚠ Admin user already exists!");
      console.log(`   Username: ${existingAdmin.username}`);
      console.log(`   Email: ${existingAdmin.email}`);
      console.log(`   Role: ${existingAdmin.role}`);
      console.log(`   Status: ${existingAdmin.status}`);
      
      // Ask if user wants to update password
      const args = process.argv.slice(2);
      if (args.includes("--force") || args.includes("-f")) {
        console.log("\n🔄 Updating admin password...");
        existingAdmin.passwordHash = "admin"; // Will be hashed by pre-save middleware
        await existingAdmin.save();
        console.log("✓ Admin password updated successfully!");
      } else {
        console.log("\n💡 Tip: Use --force or -f flag to update the password");
      }
    } else {
      // Create admin user
      const admin = new User({
        username: "admin",
        email: "admin@bookchain.com",
        passwordHash: "admin", // Will be hashed by pre-save middleware
        fullName: "Admin",
        role: "admin",
        status: "active"
      });

      await admin.save();
      console.log("✓ Admin user created successfully!");
      console.log(`   Username: ${admin.username}`);
      console.log(`   Email: ${admin.email}`);
      console.log(`   Password: admin`);
      console.log(`   Role: ${admin.role}`);
      console.log(`   Status: ${admin.status}`);
    }

    // Close connection
    await mongoose.connection.close();
    console.log("\n✓ Database connection closed");
    process.exit(0);
  } catch (error) {
    console.error("✗ Error creating admin user:", error.message);
    if (error.code === 11000) {
      console.error("   Duplicate key error - user may already exist");
    }
    await mongoose.connection.close();
    process.exit(1);
  }
};

// Run the script
createAdmin();

